# MAMoC-Android

This is a mobile computation offloading framework that offloads the compute-intensive tasks in mobile devices to more powerful
surrogates (nearby Android devices, edge devices or public cloud servers). 


## Server component installation

In order to allow offloading from mobile devices to servers, we need to first setup the servers. 

We have created two Docker images for you to deploy on your edge devices (laptops, desktops, edge routers, etc.) or 
public cloud instances (AWS, Azure, etc.). 

You can run the following commands in the terminal of your server machine:

```
docker pull dawan/mamoc_router
docker run -it -d --name "mamoc-router" -p 8080:8080 dawan/mamoc_router

docker pull dawan/mamoc_server
docker run --rm -it --name "mamoc-server" --network="host" dawan/mamoc_server

```

If you want to get the source code of the server and host it on bare metal. Navigate to [`MAMoC-Server`](https://github.com/dawand/MAMoC-Server) 
for setting up the router and the server components.


## Examples

### Text Search

This allows a user to enter a keyword and select a file size from (small, medium, and large) to find the occurrences of the word in the file . 
[Knuth-Morris-Pratt](https://www.nayuki.io/page/knuth-morris-pratt-string-matching) string searching algorithm is used. 

### Quick Sort
QuickSort is a Divide and Conquer algorithm. It picks an element as pivot and partitions the given array around the picked pivot.

### NQueens
The eight queens puzzle is the problem of placing eight chess queens on an 8×8 chessboard so that no two queens threaten each other.


## Third party libraries

### Dex Decompiling

We have performed some modifications to [Jadx Decompiler](https://github.com/skylot/jadx) to make it callable within our framework. 
We decompile all the classes and methods which are annotated with 
[@Offloadable](mamoc_client/src/main/java/uk/ac/standrews/cs/mamoc_client/Annotation/Offloadable.java) to allow them to be offloaded to external resource providers.
 
### Annotation Indexing
We use Annotation Indexing from [classindex](https://github.com/atteo/classindex) library to index all the classes annotated with
[@Offloadable](mamoc_client/src/main/java/uk/ac/standrews/cs/mamoc_client/Annotation/Offloadable.java) annotation.

### Web Application Messaging Protocol
We use [crossbar + autobahn](https://github.com/crossbario/autobahn-java) to manage the Remote Procedure Calls and Publish/Subscribe events. 
The reason we have included the source code of the library is its incompatibility with Android API < 24. 
We have used [streamsupport](https://github.com/streamsupport/streamsupport) to get the necessary functional interfaces used in Autobahn for older Android versions. 

