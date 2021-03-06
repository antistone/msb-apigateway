/*******************************************************************************
 * Copyright 2016-2017 ZTE, Inc. and others.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.onap.msb.apiroute.resources;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.onap.msb.apiroute.api.DiscoverInfo;
import org.onap.msb.apiroute.api.MicroServiceFullInfo;
import org.onap.msb.apiroute.api.exception.ExtendedInternalServerErrorException;
import org.onap.msb.apiroute.health.ConsulLinkHealthCheck;
import org.onap.msb.apiroute.health.RedisHealthCheck;
import org.onap.msb.apiroute.wrapper.util.ConfigUtil;
import org.onap.msb.apiroute.wrapper.util.HttpClientUtil;
import org.onap.msb.apiroute.wrapper.util.RouteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.codahale.metrics.health.HealthCheck.Result;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path("/services")
// @Api(tags = {"MSB-Service Resource"})
@Produces(MediaType.APPLICATION_JSON)
public class MicroServiceResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(MicroServiceResource.class);

    @Context
    UriInfo uriInfo; // actual uri info

    @GET
    @Path("/")
    @ApiOperation(value = "get all microservices ", code = HttpStatus.SC_OK, response = MicroServiceFullInfo.class,
                    responseContainer = "List")
    @ApiResponses(value = {@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    message = "get microservice List  fail", response = String.class)})
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response getMicroService() {
        String resourceUrl = "/";
        return routeHttpGet2DiscoveryService(resourceUrl);
    }

    @POST
    @Path("/")
    @ApiOperation(value = "add one microservice ", code = HttpStatus.SC_CREATED, response = MicroServiceFullInfo.class)
    @ApiResponses(value = {
                    @ApiResponse(code = HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                    message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
                    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "add microservice fail",
                                    response = String.class),
                    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST,
                                    message = "Unprocessable MicroServiceInfo JSON REQUEST", response = String.class)})
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response addMicroService(
                    @ApiParam(value = "MicroServiceInfo Instance Info",
                                    required = true) MicroServiceFullInfo microServiceInfo,
                    @Context HttpServletRequest request,
                    @ApiParam(value = "createOrUpdate",
                                    required = false) @QueryParam("createOrUpdate") @DefaultValue("true") boolean createOrUpdate,
                    @ApiParam(value = "port", required = false) @QueryParam("port") @DefaultValue("") String port)
                    throws Exception {
        return routeRegistration2DiscoveryService(microServiceInfo);
    }

    @GET
    @Path("/{serviceName}/version/{version}")
    @ApiOperation(value = "get one microservice ", code = HttpStatus.SC_OK, response = MicroServiceFullInfo.class)
    @ApiResponses(value = {
                    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "microservice not found",
                                    response = String.class),
                    @ApiResponse(code = HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                    message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
                    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "get microservice fail",
                                    response = String.class)})
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response getMicroService(
                    @ApiParam(value = "microservice serviceName") @PathParam("serviceName") String serviceName,
                    @ApiParam(value = "microservice version,if the version is empty, please enter \"null\"") @PathParam("version") @DefaultValue("") String version) {
        String resourceUrl = new StringBuilder("/").append(serviceName).append("/version/").append(version).toString();
        return routeHttpGet2DiscoveryService(resourceUrl);
    }

    @PUT
    @Path("/{serviceName}/version/{version}")
    @ApiOperation(value = "update one microservice by serviceName and version", code = HttpStatus.SC_CREATED,
                    response = MicroServiceFullInfo.class)
    @ApiResponses(value = {
                    @ApiResponse(code = HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                    message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
                    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "update microservice fail",
                                    response = String.class),
                    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST,
                                    message = "Unprocessable MicroServiceInfo JSON REQUEST", response = String.class)})
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response updateMicroService(
                    @ApiParam(value = "microservice serviceName") @PathParam("serviceName") String serviceName,
                    @ApiParam(value = "microservice version,if the version is empty, please enter \"null\"") @PathParam("version") @DefaultValue("") String version,
                    @ApiParam(value = "microservice Instance Info",
                                    required = true) MicroServiceFullInfo microServiceInfo,
                    @Context HttpServletRequest request) {

        String resourceUrl = new StringBuilder("/").append(serviceName).append("/version/").append(version).toString();
        return routeHttpPut2DiscoveryService(resourceUrl, microServiceInfo);
    }

    @DELETE
    @Path("/{serviceName}/version/{version}/nodes/{ip}/{port}")
    @ApiOperation(value = "delete single node by serviceName and version and node", code = HttpStatus.SC_NO_CONTENT)
    @ApiResponses(value = {@ApiResponse(code = HttpStatus.SC_NO_CONTENT, message = "delete node succeed "),
                    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "node not found", response = String.class),
                    @ApiResponse(code = HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                    message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
                    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "delete node fail",
                                    response = String.class)})
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response deleteNode(
                    @ApiParam(value = "microservice serviceName",
                                    required = true) @PathParam("serviceName") String serviceName,
                    @ApiParam(value = "microservice version,if the version is empty, please enter \"null\"",
                                    required = false) @PathParam("version") @DefaultValue("") String version,
                    @ApiParam(value = "ip") @PathParam("ip") String ip,
                    @ApiParam(value = "port") @PathParam("port") String port) {

        String resourceUrl = new StringBuilder("/").append(serviceName).append("/version/").append(version)
                        .append("/nodes/").append(ip).append("/").append(port).toString();
        return routeHttpDelete2DiscoveryService(resourceUrl);
    }

    @DELETE
    @Path("/{serviceName}/version/{version}")
    @ApiOperation(value = "delete one full microservice by serviceName and version", code = HttpStatus.SC_NO_CONTENT)
    @ApiResponses(value = {@ApiResponse(code = HttpStatus.SC_NO_CONTENT, message = "delete microservice succeed "),
                    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "microservice not found",
                                    response = String.class),
                    @ApiResponse(code = HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                    message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
                    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "delete microservice fail",
                                    response = String.class)})
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response deleteMicroService(
                    @ApiParam(value = "microservice serviceName",
                                    required = true) @PathParam("serviceName") String serviceName,
                    @ApiParam(value = "microservice version,if the version is empty, please enter \"null\"",
                                    required = false) @PathParam("version") @DefaultValue("") String version) {
        String resourceUrl = new StringBuilder("/").append(serviceName).append("/version/").append(version).toString();
        return routeHttpDelete2DiscoveryService(resourceUrl);
    }

    @PUT
    @Path("/{serviceName}/version/{version}/status/{status}")
    @ApiOperation(value = "update  microservice status by serviceName and version", code = HttpStatus.SC_CREATED,
                    response = MicroServiceFullInfo.class)
    @ApiResponses(value = {
                    @ApiResponse(code = HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                    message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
                    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "microservice not found",
                                    response = String.class),
                    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "update status fail",
                                    response = String.class)})
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response updateServiceStatus(
                    @ApiParam(value = "microservice serviceName",
                                    required = true) @PathParam("serviceName") String serviceName,
                    @ApiParam(value = "microservice version,if the version is empty, please enter \"null\"",
                                    required = false) @PathParam("version") @DefaultValue("") String version,
                    @ApiParam(value = "status,1：abled  0：disabled") @PathParam("status") String status) {
        String resourceUrl = new StringBuilder("/").append(serviceName).append("/version/").append(version)
                        .append("/status/").append(status).toString();
        return this.routeHttpPut2DiscoveryService(resourceUrl);
    }

    /**
     * @param discoveryUrl
     * @return
     */
    private Response routeHttpDelete2DiscoveryService(String resourceUrl) {
        String discoveryServiceUrl = getDiscoveryServiceUrl();
        String fullUrl = new StringBuilder(discoveryServiceUrl).append(resourceUrl).toString();
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse httpResponse = null;
        try {
            httpClient = HttpClients.createDefault();
            HttpDelete httpDelete = new HttpDelete(fullUrl);
            httpResponse = httpClient.execute(httpDelete);
            return cloneHttpResponse(httpResponse);
        } catch (Exception e) {
            throw new ExtendedInternalServerErrorException(e.getMessage());
        } finally {
            HttpClientUtil.closeHttpClient(httpClient, httpResponse);
        }
    }

    /**
     * @param discoveryUrl
     * @return
     */
    private Response routeHttpPut2DiscoveryService(String resourceUrl, Object bean) {
        String discoveryServiceUrl = getDiscoveryServiceUrl();
        String fullUrl = new StringBuilder(discoveryServiceUrl).append(resourceUrl).toString();
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse httpResponse = null;
        try {
            HttpPut httpPut = HttpClientUtil.createHttpPut(fullUrl, bean);
            httpClient = HttpClients.createDefault();
            httpResponse = httpClient.execute(httpPut);
            return cloneHttpResponse(httpResponse);
        } catch (Exception e) {
            throw new ExtendedInternalServerErrorException(e.getMessage());
        } finally {
            HttpClientUtil.closeHttpClient(httpClient, httpResponse);
        }
    }

    /**
     * @param discoveryUrl
     * @return
     */
    private Response routeHttpPut2DiscoveryService(String resourceUrl) {
        String discoveryServiceUrl = getDiscoveryServiceUrl();
        String fullUrl = new StringBuilder(discoveryServiceUrl).append(resourceUrl).toString();
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse httpResponse = null;
        try {
            HttpPut httpPut = HttpClientUtil.createHttpPut(fullUrl);
            httpClient = HttpClients.createDefault();
            httpResponse = httpClient.execute(httpPut);
            return cloneHttpResponse(httpResponse);
        } catch (Exception e) {
            throw new ExtendedInternalServerErrorException(e.getMessage());
        } finally {
            HttpClientUtil.closeHttpClient(httpClient, httpResponse);
        }
    }

    /**
     * @param discoveryUrl
     * @return
     */
    private Response routeHttpGet2DiscoveryService(String resourceUrl) {
        String discoveryServiceUrl = getDiscoveryServiceUrl();
        String fullUrl = new StringBuilder(discoveryServiceUrl).append(resourceUrl).toString();
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse httpResponse = null;
        try {
            HttpGet httpGet = HttpClientUtil.createHttpGet(fullUrl);
            httpClient = HttpClients.createDefault();
            httpResponse = httpClient.execute(httpGet);
            return cloneHttpResponse(httpResponse);
        } catch (Exception e) {
            throw new ExtendedInternalServerErrorException(e.getMessage());
        } finally {
            HttpClientUtil.closeHttpClient(httpClient, httpResponse);
        }
    }

    /**
     * @param microServiceFullInfo
     * @param registrationUrl
     * @return
     */
    private Response routeRegistration2DiscoveryService(MicroServiceFullInfo microServiceFullInfo) {
        String registrationUrl = getDiscoveryServiceUrl();
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse httpResponse = null;
        try {
            HttpPost httpPost = HttpClientUtil.createHttpPost(registrationUrl, microServiceFullInfo);
            httpClient = HttpClients.createDefault();
            httpResponse = httpClient.execute(httpPost);
            return cloneHttpResponse(httpResponse);
        } catch (Exception e) {
            throw new ExtendedInternalServerErrorException(e.getMessage());
        } finally {
            HttpClientUtil.closeHttpClient(httpClient, httpResponse);
        }
    }

    /**
     * @param httpResponse
     * @return
     * @throws IOException
     */
    private Response cloneHttpResponse(CloseableHttpResponse httpResponse) throws IOException {
        Response response;
        if (httpResponse.getEntity() != null) {
            String jsonString = EntityUtils.toString(httpResponse.getEntity());
            response = Response.status(httpResponse.getStatusLine().getStatusCode()).entity(jsonString).build();
        } else {
            response = Response.status(httpResponse.getStatusLine().getStatusCode()).build();
        }
        return response;
    }

    /**
     * @return
     */
    private String getDiscoveryServiceUrl() {
        DiscoverInfo discoverInfo = ConfigUtil.getInstance().getDiscoverInfo();
        String registrationUrl = new StringBuilder().append("http://").append(discoverInfo)
                        .append(RouteUtil.MSB_ROUTE_URL).toString();
        return registrationUrl;
    }

    // Discovery service doesn't provide this API, so requests to this URL are not routed to
    // discovery service
    @GET
    @Path("/health")
    @ApiOperation(value = "apigateway healthy check ", code = HttpStatus.SC_OK, response = String.class)
    @ApiResponses(value = {@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "check fail",
                    response = String.class)})
    @Produces(MediaType.TEXT_PLAIN)
    @Timed
    public Response health() {

        // redis
        Result rst = RedisHealthCheck.getResult();
        if (!rst.isHealthy()) {
            LOGGER.warn("health check failed:" + rst.getMessage());
            throw new ExtendedInternalServerErrorException(rst.getMessage());
        }

        // consul
        rst = ConsulLinkHealthCheck.getResult();
        if (!rst.isHealthy()) {
            LOGGER.warn("health check failed:" + rst.getMessage());
            throw new ExtendedInternalServerErrorException(rst.getMessage());
        }

        return Response.ok("apigateway healthy check:ok").build();
    }
}
