package com.kieronquinn.app.darq.utils

/*
 * Copyright (C) 2015 Jared Rummler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ /**
 * Gives access to the system properties store. The system properties store contains a list of
 * string key-value pairs.
 */
class SystemProperties private constructor() {
    companion object {
        private val SP = systemPropertiesClass

        /**
         * Get the value for the given key.
         */
        operator fun get(key: String?): String? {
            return try {
                SP!!.getMethod("get", String::class.java).invoke(null, key) as String
            } catch (e: Exception) {
                null
            }
        }

        /**
         * Get the value for the given key.
         *
         * @return if the key isn't found, return def if it isn't null, or an empty string otherwise
         */
        operator fun get(key: String?, def: String): String {
            return try {
                SP!!.getMethod("get", String::class.java, String::class.java)
                    .invoke(null, key, def) as String
            } catch (e: Exception) {
                def
            }
        }

        /**
         * Set the value for the given key.
         */
        operator fun set(key: String?, value: String) {
            try {
                SP!!.getMethod("set", String::class.java, String::class.java)
                    .invoke(null, key, value)
            } catch (e: Exception) {
                null
            }
        }

        /**
         * Get the value for the given key, returned as a boolean. Values 'n', 'no', '0', 'false' or
         * 'off' are considered false. Values 'y', 'yes', '1', 'true' or 'on' are considered true. (case
         * sensitive). If the key does not exist, or has any other value, then the default result is
         * returned.
         *
         * @param key the key to lookup
         * @param def a default value to return
         * @return the key parsed as a boolean, or def if the key isn't found or is not able to be
         * parsed as a boolean.
         */
        fun getBoolean(key: String?, def: Boolean): Boolean {
            return try {
                SP!!.getMethod("getBoolean", String::class.java, Boolean::class.javaPrimitiveType)
                    .invoke(null, key, def) as Boolean
            } catch (e: Exception) {
                def
            }
        }

        /**
         * Get the value for the given key, and return as an integer.
         *
         * @param key the key to lookup
         * @param def a default value to return
         * @return the key parsed as an integer, or def if the key isn't found or cannot be parsed
         */
        fun getInt(key: String?, def: Int): Int {
            return try {
                SP!!.getMethod("getInt", String::class.java, Int::class.javaPrimitiveType)
                    .invoke(null, key, def) as Int
            } catch (e: Exception) {
                def
            }
        }

        /**
         * Get the value for the given key, and return as a long.
         *
         * @param key the key to lookup
         * @param def a default value to return
         * @return the key parsed as a long, or def if the key isn't found or cannot be parsed
         */
        fun getLong(key: String?, def: Long): Long {
            return try {
                SP!!.getMethod("getLong", String::class.java, Long::class.javaPrimitiveType)
                    .invoke(null, key, def) as Long
            } catch (e: Exception) {
                def
            }
        }

        private val systemPropertiesClass: Class<*>?
            private get() = try {
                Class.forName("android.os.SystemProperties")
            } catch (shouldNotHappen: ClassNotFoundException) {
                null
            }
    }
}