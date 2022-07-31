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

import org.apache.commons.lang3.StringUtils;

import java.net.URI;

public class HttpsURLField extends Field {

    public HttpsURLField(String key, String displayName, Boolean required, String displayOrder) {
        super(key, displayName, null, required, false, displayOrder);
    }

    @Override
    public String doValidate(String input) {
        if (StringUtils.isBlank(input)) {
            return required ? this.displayName + " must not be blank." : null;
        }

        try {
            URI uri = new URI(input);
            if (uri.getScheme() == null || !uri.getScheme().equalsIgnoreCase("https")) {
                return this.displayName + " must be a valid HTTPs URL (https://example.com).";
            }
        } catch (Exception e) {
            return this.displayName + " must be a valid URL (https://example.com)";
        }

        return null;
    }

}
