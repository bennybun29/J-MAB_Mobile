package com.example.j_mabmobile.api

import com.example.j_mabmobile.model.Product
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class ProductListDeserializer : JsonDeserializer<List<Product>> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): List<Product> {
        return if (json.isJsonArray) {
            context.deserialize(json, object : TypeToken<List<Product>>() {}.type)
        } else {
            listOf(context.deserialize(json, Product::class.java))
        }
    }
}
