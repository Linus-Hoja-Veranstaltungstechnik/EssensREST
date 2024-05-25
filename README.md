# EssensREST
## Beschreibung
Dieses Artefakt bietet folgende beide Funktionalitäten:
* [Aufbauen von Rest-Schnittstellen](#aufbauen-von-rest-schnittstellen) durch Annotations nach Vorbild von JAX-RS (stark abgewandelt)
* [Ansprechen von Rest-Endpunkten](#ansprechen-von-rest-endpunkten)

---

## Aufbauen von Rest-Schnittstellen

Eine REST-Schnittstelle könnte wie folgt definiert werden:

``` java
@RestApplicationPath("/alphalightning/meta")            // vorausgesetzt:   Basis-Pfad der REST-Resource
@RestServerInfo(httpPort = 80, httpsPort = 8080)        // optional:        Setzen der Ports (default: 88 / 8888)
@SwaggerTitle("Alpha-Backend Meta")                     // optional:        Setzen des Titels der Swagger dokumentation
@Swagger                                                // optional:        Swagger generierung aktivieren
@Optional                                               // optional:        Diese REST-Resource ist optional (es wird kein Fehler geworfen falls diese nicht gestartet werden kann)
@CORS                                                   // optional:        CORS Preflight aktivieren (Wird von manchen Browsern vorausgesetzt)
@Auth(ApiKeyAuthenticator.class)                        // vorausgesetzt:   Authenticator hinzufügen, man sollte zumindest den vorgegeben nutzen, kann aber auch eigenimplementierungen hinzufügen
@SuppressWarnings("unused")                             // optional:        damit die IDE und der compiler nicht rummeckern, weil der Code nicht genutzt wird -> EssensREST magic
public class MetaBoundary extends RestApplication {     // von RestApplication erben zwingend notwendig !!!
    @GET                                                // mögliche Rest keywords: GET, PUT, POST, DELETE
    @SwaggerDescription("Can be used to check if the Service is online.")   // optional: um eine Beschreibung in Swagger hinzuzufügen
    @SwaggerResponse(description = "Service online.")                       // optional: um einen Rückgabewert in Swagger hinzuzufügen
    @Path("/ping")                                      // definieren des relative REST-Pfads
    public Response ping() {                            // Rückgabewert muss immer eine Response sein!
        
        // ...
        //
        // your code here
        //
        // ...
        
        return Response.ok("Service online");           
    }
    
    @POST
    @SwaggerDescription("Can be used to update some data.")
    @SwaggerResponse(description = "OK")
    @Path("/do-something")
    public Response doSomething(@Entity ExampleObject exampleObject) {  // sofern die Payload ein json-Objekt beinhaltet, kann dieses direkt in ein Objekt umgewandelt werden. Die Payload kann aber auch als String abgegriffen werden, indem der Objekttyp einfach String ist.
        
        // ...
        //
        // your code here
        //
        // ...
        
        return Response.ok("OK");           
    }
    
    @GET
    @SwaggerDescription("Can be used to get some data.")
    @SwaggerResponse(description = "OK")
    @Path("/do-something/{path-param}/example")
    public Response doSomething(@PathParam("path-param") String exampleParam) {  // Path-Parameter können ebenfalls direkt in primitive Objekte umgewandelt werden (String, ind, double, etc.)
        
        // ...
        //
        // your code here
        //
        // ...
        
        return Response.ok("OK");           
    }
}
```

Die entsprechenden Web-Server werden automatisch aufgebaut. Eine Firewall-Freigabe der Ports kann notwendig sein.

Die Rest-Dokumentation wird (falls die Annotation vorhanden ist) automatisch generiert und ist immer unter `http|https://<host>/swagger/<base-path>/ui` aufrufbar.

Path-Parameter und die Payload können wie im Beispiel in die Methode übergeben werden. Diese können nach belieben kombiniert werden (einmal Entity, mehrmals path-parameter).

---
## Ansprechen von Rest-Endpunkten

 to do