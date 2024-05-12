package com.example.ucar_home

import java.time.LocalDate

data class EventObject(
    private var _title: String,
    private var _imageUrl: String,
    private var _date: LocalDate,
    private var _address: String,
    private var _description: String
) {
    var title: String
        get() = _title
        set(value) {
            _title = value
        }

    var imageUrl: String
        get() = _imageUrl
        set(value) {
            _imageUrl = value
        }

    var date: LocalDate
        get() = _date
        set(value) {
            _date = value
        }

    var address: String
        get() = _address
        set(value) {
            _address = value
        }

    var description: String
        get() = _description
        set(value) {
            _description = value
        }
}
