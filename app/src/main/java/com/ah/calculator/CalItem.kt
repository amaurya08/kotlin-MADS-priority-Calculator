package com.ah.calculator

data class CalItem(val infixString: String, val result: String) {
    constructor() : this("", "")
}
