/*
 * Copyright [2021] [The Original Author]
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

package flipkart.tef.exception;

/**
 * This class is for handling exceptions being thrown from Bizlogic layer in TEF Layer and logging/propagating to upper layer
 * User: shirish.jain
 * Date: 15/06/2021
 */
public class TefExecutionException extends Exception {
    private final ErrorCode errorCode;


    public TefExecutionException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public TefExecutionException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

}
