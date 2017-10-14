/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package benchmark.testdriver;

enum ParameterType {
    PRODUCT_PROPERTY_NUMERIC(1),
    PRODUCT_FEATURE_URI(2),
    PRODUCT_TYPE_URI(3),
    CURRENT_DATE(4),
    WORD_FROM_DICTIONARY1(5),
    PRODUCT_URI(6),
    REVIEW_URI(7),
    COUNTRY_URI(8),
    OFFER_URI(9),
    CONSECUTIVE_MONTH(10),
    UPDATE_TRANSACTION_DATA(11),
    PRODUCER_URI(12),
    PRODUCT_TYPE_RANGE(13);
    private Byte value;
    ParameterType(int v) { this.value = Byte.valueOf((byte)v) ; }
}