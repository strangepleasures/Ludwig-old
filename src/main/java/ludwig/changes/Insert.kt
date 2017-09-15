package ludwig.changes

abstract class Insert : Change() {
    var parent: String? = null
    var prev: String? = null
    var next: String? = null
}
