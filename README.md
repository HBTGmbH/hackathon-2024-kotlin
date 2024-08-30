# Timetable tool (backend) for Train Sim World

## Motivation

With the release of the Public Editor for Train Sim World 4 it is now possible to create
24h timetables in the Unreal Engine and then play those in the game. However, a lot of
planning has to happen before the first service SHOULD be created in the engine.

Until now, even Dovetail Games is manually copying timetable information to Excel sheets.
This is a time-consuming process that takes the fun away and makes it a very laborious process.

What if all the timetable data of passenger services could be automatically fetched without
manual copying? This timetable tool intents to do just that.

## Limitations

Some manual work in the planning
stage will remain: creating routes with portals and depots, formations, and providing some
data for services once they are fetched. This data includes formations and linking them together
into rotations.

The public timetable isn't enough? Freight trains, empty coaching stock movements or light loco
movements are not in the timetable and have to be entered manually. But the tool will support
as good as possible.

For starters, only the Deutsche Bahn API and hence Germany are supported. More countries could
be added later on once reliable data sources are identified.

Even then, this tool can only support with present day timetables. Vintage timetables are not
available via APIs or similar sources and the manual work will remain there.
