package com.gallery.sdk.interfaces

interface GenericInterface<T> {
    fun onSuccess(data: T) {


    }

    fun data(){

    }

    fun data(data: T){

    }

    fun data(v1: T, v2: T) {

    }

    fun data(v1: T, v2: T, v3: T) {

    }

    fun onFailure(data: T) {

    }

}