/*
 * Copyright 2019 ThoughtWorks, Inc.
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

package cd.go.contrib.elasticagent;

import cd.go.contrib.elasticagent.model.Metadata;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;


public class GoServerURLMetadata extends Metadata {
    private static String GO_SERVER_URL = "go_server_url";

    public GoServerURLMetadata() {
        super(GO_SERVER_URL, true, false);
    }

    @Override
    public String doValidate(String input) {
        String validationResult = super.doValidate(input);
        if (StringUtils.isNotBlank(validationResult) || StringUtils.isBlank(input)) {
            return validationResult;
        }

        try {
            URI uri = new URI(input);
            if (uri.getHost().equalsIgnoreCase("localhost") || uri.getHost().equalsIgnoreCase("127.0.0.1")) {
                return String.format("%s must not be localhost, since this gets resolved on the agents.", GO_SERVER_URL);
            }
            if (!StringUtils.endsWith(input, "/go")) {
                return String.format("%s must be in format https://<GO_SERVER_URL>:<GO_SERVER_PORT>/go.", GO_SERVER_URL);
            }
        } catch (URISyntaxException e) {
            return String.format("%s must be a valid URL (https://example.com:8154/go).", GO_SERVER_URL);
        }

        return null;
    }
}
