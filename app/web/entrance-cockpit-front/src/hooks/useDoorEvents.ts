import { useEffect, useState } from "react";
import type { DoorEvent } from "../stores/DoorEventsStore";

export function useDoorEvents() {
  const [eventData, setEventData] = useState<DoorEvent | null>(null);

  useEffect(() => {
    const url = import.meta.env.VITE_LOGS_BACKEND_SSE_URL;
    console.log("🔌 Connecting to SSE at:", url);

    const eventSource = new EventSource(url);

    // Confirm connection
    eventSource.onopen = () => {
      console.log("🟢 SSE connected");
    };

    // Listen to the "access-event" SSE event
    eventSource.addEventListener("access-event", (rawEvent: MessageEvent) => {
      try {
        console.log("🔔 SSE event:", rawEvent.data);

        const data = JSON.parse(rawEvent.data);

        const parsedEvent: DoorEvent = {
          badgeId: data.badgeId ?? undefined,
          firstName: data.firstName,
          lastName: data.lastName,
          eventType: data.eventType,
          timestamp: data.timestamp ?? new Date().toISOString(),
        };

        setEventData(parsedEvent);
      } catch (err) {
        console.error("❌ Failed to parse SSE event:", err);
      }
    });

    // Catch unexpected messages
    eventSource.onmessage = (e) => {
      console.log("📨 Default SSE message:", e.data);
    };

    // Handle errors
    eventSource.onerror = (err) => {
      console.warn("⚠️ SSE connection error:", err);
    };

    return () => {
      console.log("🔌 Closing SSE connection");
      eventSource.close();
    };
  }, []);

  return eventData;
}