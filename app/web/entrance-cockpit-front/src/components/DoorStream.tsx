import { DoorOpen, Wifi, WifiOff } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { doorEventsStore } from "../stores/DoorEventsStore";
import { observer } from "mobx-react-lite";
import { Badge } from "./ui/badge";


function DoorStream() {
  const isConnected = doorEventsStore.connectionStatus == 'connected';
  const events = doorEventsStore.events!;

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle>Live Stream</CardTitle>
          <Badge variant={isConnected ? "default" : "secondary"}>
            {isConnected ? (
              <div className="flex items-center gap-1">
                <Wifi className="h-3 w-3" />
                Connected
              </div>
            ) : (
              <div className="flex items-center gap-1">
                <WifiOff className="h-3 w-3" />
                Disconnected
              </div>
            )}
          </Badge>
        </div>
      </CardHeader>
      <CardContent>
        {events.length > 0 ? (
          events.map((event) => (
            <div
              key={event.badgeId}
              className="flex items-center gap-4 p-4 bg-muted rounded-lg mb-4"
            >
              <DoorOpen className="h-8 w-8 text-green-600" />
              <div>
                <p className="font-medium">{`${event.firstName} ${event.lastName}`}</p>
                <p className="text-sm text-muted-foreground">
                  "Opened the door"
                  • {new Date(event.timestamp).toLocaleTimeString()}
                </p>
              </div>
            </div>
          ))
        ) : (
          <p className="text-muted-foreground text-center py-8">
            Waiting for door events...
          </p>
        )}

      </CardContent>
    </Card>
  );
}
export default observer(DoorStream);