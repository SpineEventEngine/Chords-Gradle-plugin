/*
 * Copyright 2023, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.internal.dependency

@Suppress("unused", "ConstPropertyName")
object KotlinX {

    const val group = "org.jetbrains.kotlinx"

    object Coroutines {

        // https://github.com/Kotlin/kotlinx.coroutines
        const val version = "1.7.3"
        const val core = "$group:kotlinx-coroutines-core:$version"
        const val jdk8 = "$group:kotlinx-coroutines-jdk8:$version"

        // TODO:2024-08-30:dmitry.pikhulya: the following four libs were added
        //    relative to the `config` module's content. Consider updating
        //    the `config` module accordingly.
        //   See https://github.com/SpineEventEngine/Chords/issues/3
        const val bom = "$group:kotlinx-coroutines-bom:$version"
        const val test = "$group:kotlinx-coroutines-test:$version"
        const val testJvm = "$group:kotlinx-coroutines-test-jvm:$version"
        const val debug = "$group:kotlinx-coroutines-debug:$version"
    }

    // TODO:2024-08-30:dmitry.pikhulya: This AtomicFu library was added relative
    //   to the original `config` module's content. Consider updating `config`.
    //   See https://github.com/SpineEventEngine/Chords/issues/3
    // https://github.com/Kotlin/kotlinx-atomicfu
    object AtomicFu {
        const val version = "0.7"
        const val lib = "$group:atomicfu:$version"
    }
}