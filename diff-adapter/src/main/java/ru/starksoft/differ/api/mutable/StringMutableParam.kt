package ru.starksoft.differ.api.mutable

class StringMutableParam : ObjectMutableParam<String>() {

    override fun toString(): String {
        return "StringMutableParam() ${super.toString()}"
    }
}
