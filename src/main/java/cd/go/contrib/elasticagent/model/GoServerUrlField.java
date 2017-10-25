/*
 * Copyright 2017 ThoughtWorks, Inc.
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
import java.net.URISyntaxException;

public class GoServerUrlField extends SecureURLField {
    public GoServerUrlField(String key, String displayName, Boolean required, String displayOrder) {
        super(key, displayName, required, displayOrder);
    }

    @Override
    public String doValidate(String input)  {
        String validationResult = super.doValidate(input);
        if(StringUtils.isNotBlank(validationResult) || StringUtils.isBlank(input)) {
            return validationResult;
        }

        try {
            URI uri = new URI(input);
            if (uri.getHost().equalsIgnoreCase("localhost") || uri.getHost().equalsIgnoreCase("127.0.0.1")) {
                return this.displayName + " must not be localhost, since this gets resolved on the agents.";
            }
            if (!StringUtils.endsWith(input, "/go")) {
                return this.displayName + " must be in format https://<GO_SERVER_URL>:<GO_SERVER_PORT>/go.";
            }
        } catch (URISyntaxException e) {
            return this.displayName + " must be a valid URL (https://example.com:8154/go).";
        }

        return null;
    }
}
