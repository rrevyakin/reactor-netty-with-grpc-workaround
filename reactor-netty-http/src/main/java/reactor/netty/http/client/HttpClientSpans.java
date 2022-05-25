/*
 * Copyright (c) 2022 VMware, Inc. or its affiliates, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package reactor.netty.http.client;

import io.micrometer.common.docs.KeyName;
import io.micrometer.tracing.docs.DocumentedSpan;

/**
 * {@link HttpClient} spans.
 *
 * @author Violeta Georgieva
 * @since 1.1.0
 */
enum HttpClientSpans implements DocumentedSpan {

	/**
	 * Response Span.
	 */
	HTTP_CLIENT_RESPONSE_SPAN {
		@Override
		public String getName() {
			return "%s";
		}

		@Override
		public KeyName[] getKeyNames() {
			return ResponseTimeHighCardinalityTags.values();
		}

		@Override
		public Enum<?> overridesDefaultSpanFrom() {
			return HttpClientObservations.HTTP_CLIENT_RESPONSE_TIME;
		}
	};

	enum ResponseTimeHighCardinalityTags implements KeyName {

		/**
		 * Reactor Netty protocol (always http).
		 */
		REACTOR_NETTY_PROTOCOL {
			@Override
			public String getKeyName() {
				return "reactor.netty.protocol";
			}
		},

		/**
		 * Reactor Netty status.
		 */
		REACTOR_NETTY_STATUS {
			@Override
			public String getKeyName() {
				return "reactor.netty.status";
			}
		},

		/**
		 * Reactor Netty type (always client).
		 */
		REACTOR_NETTY_TYPE {
			@Override
			public String getKeyName() {
				return "reactor.netty.type";
			}
		},

		/**
		 * Remote address.
		 */
		REMOTE_ADDRESS {
			@Override
			public String getKeyName() {
				return "remote.address";
			}
		}
	}
}