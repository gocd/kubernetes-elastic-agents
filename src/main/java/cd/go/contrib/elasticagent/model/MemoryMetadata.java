/*
 * Copyright 2022 Thoughtworks, Inc.
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

package cd.go.contrib.elasticagent.model;


import cd.go.contrib.elasticagent.utils.Size;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MemoryMetadata extends Metadata {

    public MemoryMetadata(String key, boolean required) {
        super(key, required, false);
    }

    @Override
    protected String doValidate(String input) {
        List<String> errors = new ArrayList<>(Arrays.asList(super.doValidate(input)));

        try {
            Size.parse(input);
        } catch (Exception e) {
            errors.add(e.getMessage());
        }

        errors.removeAll(Collections.singleton(null));

        if (errors.isEmpty()) {
            return null;
        }
        return String.join(". ", errors);
    }
}
