/*
 * Copyright [2024] [The Original Author]
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

package flipkart.tef.bizlogics;

import flipkart.tef.exception.TefExecutionException;

import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * A DataAdapter which emits a future. During the injection phase the caller can either
 * 1) do a blocking get (thread will stall till the result is available)
 * 2) do a done and get check (to consume the result only if its available)
 * 3) do a get with timeout (to wait for the result before consuming them)
 * <p>
 * The semantics of the Generic Type parameter expect a `Future<Optional<X>>` explicitly
 * instead of just <X> because during the flow building phase, the complete signature of the
 * generic type interface needs to be present at impl or superclass.
 * Only taking the final result type as input from implementation classes (for the value of generic parameter)
 * will break that contract.
 * <p>
 * Since the flow builder uses reflection to get generic params to know what will the return type of data adapter,
 * taking only final result type (say X) from implementation class, will appear to TEF as if
 * the implementation classes return 'X' rather than the Future.
 * <p>
 * <p>
 * Date: 18/06/22
 * Time: 7:42 PM
 */
public abstract class AsyncDataAdapterBizlogic<T extends Future<Optional<U>>, U> extends DataAdapterBizlogic<T> {

    private final ThreadPoolExecutor threadPoolExecutor;
    private final boolean bubbleException;

    /**
     * @param threadPoolExecutor Threadpool executor to which to task will be submitted
     */
    public AsyncDataAdapterBizlogic(ThreadPoolExecutor threadPoolExecutor) {
        this(threadPoolExecutor, false);
    }

    /**
     * @param threadPoolExecutor Threadpool executor to which to task will be submitted
     * @param bubbleException    if true, any exception thrown as part of computing the result will be rethrown,
     *                           else `Optional.empty` will be returned.
     */
    public AsyncDataAdapterBizlogic(ThreadPoolExecutor threadPoolExecutor, boolean bubbleException) {
        this.threadPoolExecutor = threadPoolExecutor;
        this.bubbleException = bubbleException;
    }

    @Override
    public final T adapt(final TefContext tefContext) {
        /*
        Submit the task to the threadpool
        The `bubbleException` flag will be used to decide the behavior in case of an exception,
            either to return an empty value, or rethrow the exception
         */
        return (T) threadPoolExecutor.submit(() -> getResultImpl(tefContext));
    }

    /**
     * This method should compute and return the result. The flow execution will not be blocked on this method.
     * i.e. This method will run in async
     *
     * @param tefContext TefContext for callers
     * @return The result
     * @throws TefExecutionException The retriability error codes are not honoured when executing this bizlogic
     */
    public abstract U getResult(TefContext tefContext) throws TefExecutionException;

    private Optional<U> getResultImpl(TefContext tefContext) throws Exception {
        try {
            U result = getResult(tefContext);
            return Optional.ofNullable(result);
        } catch (Exception e) {
            // Catch-all block to minimize side effects
            if (bubbleException) {
                throw e;
            } else {
                return Optional.empty();
            }
        }
    }
}
