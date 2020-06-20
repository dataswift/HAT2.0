package org.hatdex.hat.utils

import io.dataswift.adjudicator.Types.{ Contract, ContractId }
import play.api.http.HttpVerbs
import play.api.libs.json.Json
import play.api.libs.ws.{ WSClient, WSRequest, WSResponse }

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.Future

// TODO: rename to adjudicator
// TODO: details from config
object NetworkRequest {

  //get the URL from config
  def getPublicKey(
    contractId: ContractId,
    hatName: String,
    keyId: String,
    ws: WSClient)(implicit ec: ExecutionContext): Future[WSResponse] = {
    // TODO: update this endpoint
    //ContractIdVar(contractId) / "hat" / HatVar(hatName) / KeyIdVar(keyId) => {
    val url =
      s"http://localhost:9002/v1/contracts/${contractId}/hat/${hatName}/${keyId}"
    println(url)
    val req = makeRequest(url, ws)
    req.get()
  }

  def createContract(contract: Contract, ws: WSClient)(
    implicit
    ec: ExecutionContext): Future[WSResponse] = {
    val url = s"http://localhost:9002/v1/contracts"
    println(url)
    val req = makeRequest(url, ws)
    req.put(s"""{"contract":"${contract.contractUUID}"}""")
  }

  def joinContract(hatName: String, contractId: ContractId, ws: WSClient)(
    implicit
    ec: ExecutionContext): Future[WSResponse] = {
    val url = s"http://localhost:9002/v1/contracts/${contractId}/hat/${hatName}"
    println(url)
    val req =
      makeRequest(url, ws).withHttpHeaders("Content-Type" -> "application/json")
    req.put("d")
  }

  def leaveContract(hatName: String, contractId: ContractId, ws: WSClient)(
    implicit
    ec: ExecutionContext): Future[WSResponse] = {
    val url = s"http://localhost:9002/v1/contracts/${contractId}/hat/${hatName}"
    println(url)
    val req = makeRequest(url, ws)
    req.delete()
  }

  // TODO: pass in the verb
  private def makeRequest(url: String, ws: WSClient)(
    implicit
    ec: ExecutionContext): WSRequest = {
    ws.url(url)
    //.withHttpHeaders("Accept" -> "application/json"
    //, "X-Auth-Token" -> hatSharedSecret)
    // )
    //request.get()
  }
}
