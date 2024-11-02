/*
 * Hornet Queen
 * Copyright (c) 2024 Sascha Strauß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.dreamcube.hornet_queen

const val BYTE_BITS = 8
const val BYTE_SIZE = 1
const val SHORT_SIZE = 2
const val CHAR_SIZE = 2
const val INT_SIZE = 4
const val LONG_SIZE = 8
const val FLOAT_SIZE = 4
const val DOUBLE_SIZE = 8
const val UUID_SIZE = 16
const val SHORT_SHIFT = 1
const val CHAR_SHIFT = 1
const val INT_SHIFT = 2
const val LONG_SHIFT = 3
const val FLOAT_SHIFT = 2
const val DOUBLE_SHIFT = 3
const val UUID_SHIFT = 4
const val NO_INDEX: Int = -1

object ConfigurableConstants {
    const val DEFAULT_INITIAL_SIZE = 11
    const val DEFAULT_LOAD_FACTOR: Double = 0.75
    const val DEFAULT_NATIVE: Boolean = true
}
