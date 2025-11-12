import { makeAutoObservable } from "mobx";

export interface DoorEvent {
  badgeId?: string;
  name: string;
  state: "open" | "closed";
  timestamp: string;
}

class DoorEventsStore {
  events: DoorEvent[] = [];
  connectionStatus: "connected" | "disconnected" = "disconnected";

  constructor() {
    makeAutoObservable(this);
  }

  // This method encapsulates the logic for adding a new event.
  addEvent(event: DoorEvent) {
    // Keep the list trimmed to the last 10 events
    this.events = [event, ...this.events].slice(0, 10);
  }

  connectToSSE(url: string) {
    const eventSource = new EventSource(url);

    eventSource.onopen = () => {
      this.connectionStatus = "connected";
      console.log("✅ SSE connected");
    };

    eventSource.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        console.log("📨 SSE event received:", data);

        const newEvent: DoorEvent = {
          name: data.name ?? "Unknown",
          state: data.state ?? "closed",
          timestamp: new Date().toISOString(),
        };

        // ✅ 2. USE THE NEW METHOD HERE
        // This makes the code cleaner and fixes the error.
        this.addEvent(newEvent);

      } catch (err) {
        console.error("Failed to parse SSE message:", err);
      }
    };

    eventSource.onerror = () => {
      this.connectionStatus = "disconnected";
      console.warn("⚠️ SSE connection lost");
    };
  }

  getEvents() {
    return this.events;
  }
}

export const doorEventsStore = new DoorEventsStore();