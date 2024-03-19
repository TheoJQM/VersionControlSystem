package svcs
import java.io.File
import java.security.MessageDigest
import java.util.*

val vcs = File("./vcs")
val config = File("./vcs/config.txt")
val index = File("./vcs/index.txt")
val commits = File("./vcs/commits")
val log = File("./vcs/log.txt")

val help = """
        These are SVCS commands:
        config     Get and set a username.
        add        Add a file to the index.
        log        Show commit logs.
        commit     Save changes.
        checkout   Restore a file.
    """.trimIndent()

val commands = mapOf("config" to "Please, tell me who you are.",
    "--help" to help, "add" to "Add a file to the index.",
    "log" to "Show commit logs.", "commit" to "Save changes.",
    "checkout" to "Restore a file."
)

fun main(args: Array<String>) {
    createFiles()

    when {
        args.isEmpty() -> println(commands["--help"])
        args.first() in commands.keys -> handleCommand(args)
        args.first() !in commands.keys -> println("\'${ args.first() }\' is not a SVCS command.")
    }
}

fun handleCommand(args: Array<String>) {
    when (args[0]) {
        "config" -> if (args.size == 2) updateName(args[1]) else getName()
        "add" -> if (args.size == 2) updateTrackedFiles(args[1]) else getTrackedFiles()
        "commit" -> if (args.size == 2) commit(args[1]) else println("Message was not passed.")
        "log" -> if (args.size == 1) log()
        "checkout" -> if (args.size == 2) checkout(args[1]) else println("Commit id was not passed.")
        else -> println(commands[args.first()])
    }
}

fun createFiles() {
    vcs.mkdir()
    commits.mkdir()
    index.createNewFile()
    config.createNewFile()
    log.createNewFile()
}

fun updateName(name: String) {
    config.writeText(name)
    println("The username is $name.")
}

fun getName() {
    if (config.readText() == "" ) {
        println("Please, tell me who you are.")
    } else {
        print("The username is ${ config.readText() }.")
    }
}

fun updateTrackedFiles(name: String) {
    if (File("./$name").exists()) {
        index.appendText("$name\n")
        println("The file \'$name\' is tracked.")
    } else {
        println("Can't find \'$name\'.")
    }
}

fun getTrackedFiles() {
    if (index.readText() == "" ) {
        println("Add a file to the index.")
    } else {
        println("Tracked files:")
        index.readLines().forEach(::println)
    }
}

fun commit(message: String) {
    if (commits.list()?.isEmpty() == true) {
        createNewCommit(message)
    } else {
        val oldCommitFiles = File(commits.list()!!.last())
        createNewCommit(message, oldCommitFiles, false)
    }
}

fun createNewCommit(message: String, oldCommit: File = File(""), firstCommit: Boolean = true) {
    val trackedFile = index.readLines()

    val md = MessageDigest.getInstance("sha1")
    val hashing = md.digest(log.readBytes())
    val hash = HexFormat.of().formatHex(hashing)

    val newCommit = File("$commits/$hash")
     newCommit.mkdir()

    if (firstCommit) {
        trackedFile.forEach { file ->
            val newFile = File("$newCommit/$file")
            File("./$file").copyTo(newFile)
        }
        newLog(hash, message)
    } else {
        val contentChanged = trackedFile.any { fileName ->
            val oldFileContent = File("$commits/$oldCommit/$fileName").readText()
            val currentFileContent = File("./$fileName").readText()
            oldFileContent != currentFileContent
        }

        if (contentChanged) {
            trackedFile.forEach {file ->
                val newFile = File("$newCommit/$file")
                File("./$file").copyTo(newFile)
            }
            newLog(hash, message)
        } else {
            println("Nothing to commit.")
        }
    }
}

fun newLog(commitID: String, message: String) {
    val author = config.readText()

    val oldLog = log.readText()
    val newLog = """
        commit $commitID
        Author: $author
        $message
        
        """.trimIndent()

    log.writeText(newLog)
    log.appendText(oldLog)
    println("Changes are committed.")
}

fun log() {
    if (log.readText() == "") {
        println("No commits yet.")
    } else {
        log.readLines().forEach(::println)
    }
}

fun checkout(id: String) {
    val commit = File("$commits/$id")
    if (!commit.exists()) {
        println("Commit does not exist.")
    } else {
        commit.list()?.forEach {
            File("$commit/$it").copyTo(File("./$it"), true)
        }
        println("Switched to commit $id.")
    }
}