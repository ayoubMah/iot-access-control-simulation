import { useState } from "react";
import { DoorOpen, DoorClosed } from "lucide-react";
import { Button } from "./ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { toast } from "sonner";

export default function ManualOperations() {
  const [loading, setLoading] = useState(false);

const manualEvent = async (eventType: "open" | "close") => {
  try {
    setLoading(true);
    const res = await fetch(`/api/manual/${eventType}`, { method: "POST" });
    if (res.ok) {
      toast.success(`Door ${eventType === "open" ? "opened" : "closed"} successfully`);
    } else {
      toast.error("Command failed");
    }
  } catch (err) {
    console.error("Failed to simulate event:", err);
    toast.error("Failed to send command");
  } finally {
    setLoading(false);
  }
};


  return (
    <Card>
      <CardHeader>
        <CardTitle>Simulate Door Events</CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">

        <div className="flex gap-2">
          <Button
            onClick={() => manualEvent("open")}
            disabled={loading}
            className="flex-1"
            variant="default"
          >
            <DoorOpen className="h-4 w-4 mr-2" />
            Open Door
          </Button>
          <Button
            onClick={() => manualEvent("close")}
            disabled={loading}
            className="flex-1"
            variant="secondary"
          >
            <DoorClosed className="h-4 w-4 mr-2" />
            Close Door
          </Button>
        </div>

        <p className="text-sm text-muted-foreground text-center">
          Manually open or close the gate.
        </p>
      </CardContent>
    </Card>
  );
}
