import { DoorOpen, DoorClosed, RefreshCw } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Button } from "./ui/button";
import { type DoorEvent } from "../stores/DoorEventsStore";
import { useState } from "react";

function DoorHistory() {
  const [events, setEvents] = useState<DoorEvent[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  const refreshHistory = async () => {
    setIsLoading(true);
    setEvents([]);
    setTimeout(() => {
      setIsLoading(false);
    }, 3000);;
  };
  
  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle>Door Event History</CardTitle>
          <Button
            variant="outline"
            size="sm"
            onClick={() => refreshHistory()}
            disabled={isLoading}
          >
            {isLoading && <RefreshCw className={`h-4 w-4 mr-2 animate-spin`} />}
            Refresh
          </Button>
        </div>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <p className="text-muted-foreground text-center py-8">
            Loading history...
          </p>
        ) : events.length === 0 ? (
          <p className="text-muted-foreground text-center py-8">
            No door events recorded yet
          </p>
        ) : (
          <div className="space-y-2">
            {events.map((event) => (
              <div
                key={event.badgeId}
                className="flex items-center gap-4 p-3 rounded-lg hover:bg-muted transition-colors"
              >
                {event.state === "open" ? (
                  <DoorOpen className="h-5 w-5 text-green-600 shrink-0" />
                ) : (
                  <DoorClosed className="h-5 w-5 text-red-600 shrink-0" />
                )}
                <div className="flex-1 min-w-0">
                  <p className="font-medium truncate">{event.name}</p>
                  <p className="text-sm text-muted-foreground">
                    {event.state === "open"
                      ? "Opened the door"
                      : "Closed the door"}
                  </p>
                </div>
                <p className="text-sm text-muted-foreground whitespace-nowrap">
                  {new Date(event.timestamp).toLocaleString()}
                </p>
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  );
}

export default DoorHistory;
