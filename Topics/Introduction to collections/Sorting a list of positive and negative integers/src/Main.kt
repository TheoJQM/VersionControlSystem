import java.util.*

fun main() {
    val numberList = mutableListOf<Int>()
    val scanner = Scanner(System.`in`)
    numberList.addAll(scanner.nextLine().split(" ").map { num -> num.toInt() })
    numberList.sort()
    for (num in numberList) print("$num ")
}