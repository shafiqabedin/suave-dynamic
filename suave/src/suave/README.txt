I really want to refactor and slim down this huge stinking pile of
source.  Well, it's really not that huge but still.

Ideally, I want to extract a bunch of source into several additional
'libraries' of code that SUAVE would then use.  These would include a)
OpenGL and 3D stuff b) stuff involving DEMs and georeferencing c)
other utils (sockets, strings).

ALSO, more importantly really, I'd really really like to give SUAVE a
socket interface/API and move all stuff involving image feeds and
integration with CoT or Sanjaya, out of SUAVe.  To clarify, SUAVE
would have sockets for; 

1) incoming imagery and positioning of such imagery as completely as possible

2) incoming 'state' - i.e. DIS or CoT style 'entity state' messages
about entities in the world

3) outgoing user events - i.e. user clicked on something or drew an
outline or whatever.




Junk that may well not be in use at all, or may simply not work, or something that was exploratory
--------------------

Feedback.java
Console.java
TerrainFactory.java
TestCollide.java


Priority 1 candidates for extraction
--------------------

DisplayListFactory.java - probably junk this, displaylist are bad m'kay?
Axis.java - draw the axis - to make my life easier debugging
Box.java - draw a box in immediate mode - junk this probably
GeomUtil.java
MollerTrumbore.java
Mat44.java
Rot44.java
Renderable.java
RenderableDisplayList.java
RenderableInterleavedVBO.java
RenderableVBO.java
RenderableVBOFactory.java
RenderableVideo.java
RenderableVideoFactory.java
PosRot.java
Vec3f.java

Priority 2 candidates for extraction - there's some stuff IN these
classes that can and should be cleanly extracted, but some other stuff
in there that is app specific or needs to be refactored.
--------------------

Select.java


Priority 3 candidates for extraction - not quite ready for prime time
but something to consider
--------------------

Rasterize.java - some nice triangle/barycentric coord stuff in here

Spatial.java  - crude spatial hash - but it works!  Hashes things into 'screen' space.

Spatial2.java - experimental variation of Spatial.

TextureDB.java - this and TextureInfo are handy and somewhat useful but may need to be generalized some...
TextureInfo.java

Projection.java - this is doing some deep shit - it needs to be
refactored seriously before it's general enoough to extract but I get
the feeling it's something that'd be useful.

Skybox.java


OGL stuff that is kinda the basic framework, that I probably found in some example off the web
--------------------

ExceptionHandler.java
GLDisplay.java
GLError.java
BitmapLoader.java
GLCamera.java
HelpOverlay.java
InputHandler.java
ResourceRetriever.java
TextureReader.java


SUAVE specific stuff?
--------------------

Baker.java
BakerCommand.java
FrameList.java
CaptureCommand.java
CaptureReply.java
Message.java
SkyAndGround.java
Painter.java
ExtraRenderables.java - useful to look at to look at how to use the more general stuff.
Main.java
Mesh.java
Model.java
Origin.java
Renderer.java
State.java
StateDB.java
StateEnums.java
TimeConverter.java
Triangle.java
UAVCamera.java
Vertex.java
VideoFrame.java
VideoGenerator.java

Lights.java - doesn't really do anything yet

Utils and socket stuff
--------------------

Debug.java
Client.java
GeoTransforms.java
GeoTransformsConstants.java
GeoTransformsLVCS.java
SanjayaListener.java
Server.java
Simulate.java
StringUtils.java


DEM stuff
--------------------
DEM.java - holds the loaded DEM data
DEMFactory.java - load the DEM data, in general
DEMxyz.java - simple 'xyz' format file
DemAndTexture.java - leftover, everything has been factored out
GeoTIFF.java
GeoTexture.java
GeoTextureFactory.java

Stuff involving log files
--------------------

Feed.java
VirtualCockpitLogLine.java
vcdecode.java


