package ru.fedosov.email.parser

data class Molecule(val fileName: String,
                    var reCalculateCBS: ArrayList<Int> = arrayListOf(),
                    var reCalculatePM6: ArrayList<Int> = arrayListOf(),
                    var reCalculate6111: ArrayList<Int> = arrayListOf())