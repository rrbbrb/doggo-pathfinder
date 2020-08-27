import kotlinx.browser.document
import react.dom.*

data class Node(val row: Int, val col: Int)

fun main() {
    render(document.getElementById("root")) {
        child(App::class) {}
    }
}