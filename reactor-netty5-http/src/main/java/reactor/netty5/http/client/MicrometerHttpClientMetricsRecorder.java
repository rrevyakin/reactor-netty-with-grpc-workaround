/*
 * Copyright (c) 2019-2022 VMware, Inc. or its affiliates, All Rights Reserved.
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
package reactor.netty5.http.client;

import io.micrometer.core.instrument.Timer;
import reactor.netty5.Metrics;
import reactor.netty5.channel.MeterKey;
import reactor.netty5.http.MicrometerHttpMetricsRecorder;
import reactor.netty5.internal.util.MapUtils;
import reactor.util.annotation.Nullable;

import java.net.SocketAddress;
import java.time.Duration;

import static reactor.netty5.Metrics.DATA_RECEIVED_TIME;
import static reactor.netty5.Metrics.DATA_SENT_TIME;
import static reactor.netty5.Metrics.HTTP_CLIENT_PREFIX;
import static reactor.netty5.Metrics.METHOD;
import static reactor.netty5.Metrics.REGISTRY;
import static reactor.netty5.Metrics.REMOTE_ADDRESS;
import static reactor.netty5.Metrics.RESPONSE_TIME;
import static reactor.netty5.Metrics.STATUS;
import static reactor.netty5.Metrics.URI;

/**
 * @author Violeta Georgieva
 * @since 0.9
 */
final class MicrometerHttpClientMetricsRecorder extends MicrometerHttpMetricsRecorder implements HttpClientMetricsRecorder {

	final static MicrometerHttpClientMetricsRecorder INSTANCE = new MicrometerHttpClientMetricsRecorder();

	private MicrometerHttpClientMetricsRecorder() {
		super(HTTP_CLIENT_PREFIX, "http");
	}

	@Override
	public void recordDataReceivedTime(SocketAddress remoteAddress, String uri, String method, String status, Duration time) {
		String address = Metrics.formatSocketAddress(remoteAddress);
		MeterKey meterKey = new MeterKey(uri, address, method, status);
		Timer dataReceivedTime = MapUtils.computeIfAbsent(dataReceivedTimeCache, meterKey,
				key -> filter(Timer.builder(name() + DATA_RECEIVED_TIME)
				                   .tags(HttpClientMeters.DataReceivedTimeTags.REMOTE_ADDRESS.getKeyName(), address,
				                         HttpClientMeters.DataReceivedTimeTags.URI.getKeyName(), uri,
				                         HttpClientMeters.DataReceivedTimeTags.METHOD.getKeyName(), method,
				                         HttpClientMeters.DataReceivedTimeTags.STATUS.getKeyName(), status)
				                   .register(REGISTRY)));
		if (dataReceivedTime != null) {
			dataReceivedTime.record(time);
		}
	}

	@Override
	public void recordDataSentTime(SocketAddress remoteAddress, String uri, String method, Duration time) {
		String address = Metrics.formatSocketAddress(remoteAddress);
		MeterKey meterKey = new MeterKey(uri, address, method, null);
		Timer dataSentTime = MapUtils.computeIfAbsent(dataSentTimeCache, meterKey,
				key -> filter(Timer.builder(name() + DATA_SENT_TIME)
				                   .tags(HttpClientMeters.DataSentTimeTags.REMOTE_ADDRESS.getKeyName(), address,
				                         HttpClientMeters.DataSentTimeTags.URI.getKeyName(), uri,
				                         HttpClientMeters.DataSentTimeTags.METHOD.getKeyName(), method)
				                   .register(REGISTRY)));
		if (dataSentTime != null) {
			dataSentTime.record(time);
		}
	}

	@Override
	public void recordResponseTime(SocketAddress remoteAddress, String uri, String method, String status, Duration time) {
		String address = Metrics.formatSocketAddress(remoteAddress);
		Timer responseTime = getResponseTimeTimer(name() + RESPONSE_TIME, address, uri, method, status);
		if (responseTime != null) {
			responseTime.record(time);
		}
	}

	@Nullable
	final Timer getResponseTimeTimer(String name, String address, String uri, String method, String status) {
		MeterKey meterKey = new MeterKey(uri, address, method, status);
		return MapUtils.computeIfAbsent(responseTimeCache, meterKey,
				key -> filter(Timer.builder(name)
						.tags(REMOTE_ADDRESS, address, URI, uri, METHOD, method, STATUS, status)
						.register(REGISTRY)));
	}
}