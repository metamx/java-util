/*
 * Copyright 2011 - 2015 Metamarkets Group Inc.
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

package com.metamx.http.client.response;

import com.google.common.base.Throwables;
import com.metamx.http.client.io.AppendableByteArrayInputStream;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpResponse;

import java.io.InputStream;

/**
 */
public class InputStreamResponseHandler implements HttpResponseHandler<AppendableByteArrayInputStream, InputStream>
{
  @Override
  public ClientResponse<AppendableByteArrayInputStream> handleResponse(HttpResponse response)
  {
    AppendableByteArrayInputStream in = new AppendableByteArrayInputStream();
    in.add(getContentBytes(response.getContent()));
    return ClientResponse.finished(in);
  }

  @Override
  public ClientResponse<AppendableByteArrayInputStream> handleChunk(
      ClientResponse<AppendableByteArrayInputStream> clientResponse, HttpChunk chunk
  )
  {
    clientResponse.getObj().add(getContentBytes(chunk.getContent()));
    return clientResponse;
  }

  @Override
  public ClientResponse<InputStream> done(ClientResponse<AppendableByteArrayInputStream> clientResponse)
  {
    final AppendableByteArrayInputStream obj = clientResponse.getObj();
    obj.done();
    return ClientResponse.<InputStream>finished(obj);
  }

  @Override
  public void exceptionCaught(
      ClientResponse<AppendableByteArrayInputStream> clientResponse,
      Throwable e
  )
  {
    final AppendableByteArrayInputStream obj = clientResponse.getObj();
    obj.exceptionCaught(e);
  }

  private byte[] getContentBytes(ChannelBuffer content)
  {
    byte[] contentBytes = new byte[content.readableBytes()];
    content.readBytes(contentBytes);
    return contentBytes;
  }
}
