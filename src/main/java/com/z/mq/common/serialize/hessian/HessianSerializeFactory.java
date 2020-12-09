/**
 * Copyright (C) 2016 Newland Group Holding Limited
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.z.mq.common.serialize.hessian;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class HessianSerializeFactory extends BasePooledObjectFactory<HessianSerializer> {

    @Override
    public HessianSerializer create() throws Exception {
        return createHessian();
    }

    @Override
    public PooledObject<HessianSerializer> wrap(HessianSerializer hessian) {
        return new DefaultPooledObject<HessianSerializer>(hessian);
    }

    private HessianSerializer createHessian() {
        return new HessianSerializer();
    }
}

