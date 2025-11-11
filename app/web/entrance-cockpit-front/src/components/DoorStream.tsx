import { useEffect, useState } from "react";
import { DoorOpen, DoorClosed, Wifi, WifiOff } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { doorEventsStore, type DoorEvent } from "../stores/DoorEventsStore";
import { observer } from "mobx-react-lite";
import { Badge } from "./ui/badge";

interface DoorStreamProps {
  onEvent: (event: DoorEvent) => void;
}

function DoorStream({ onEvent }: DoorStreamProps) {
  const [isConnected, setIsConnected] = useState(doorEventsStore.connectionStatus == 'connected');
  const [lastEvent, setLastEvent] = useState<DoorEvent | null>(null);
  const events = doorEventsStore.getEvents()!;

  useEffect(() => {
    setLastEvent(events[0]);
  }, [events]);

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
              {event.state === "open" ? (
                <DoorOpen className="h-8 w-8 text-green-600" />
              ) : (
                <DoorClosed className="h-8 w-8 text-red-600" />
              )}

              <div>
                <p className="font-medium">{event.name}</p>
                <p className="text-sm text-muted-foreground">
                  {event.state === "open"
                    ? "Opened the door"
                    : "Closed the door"}{" "}
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
