import { makeAutoObservable } from "mobx";

export interface DoorEvent {
  badgeId?: string | null;
  firstName: string;
  lastName: string;
  eventType: 'badge' | 'manual';
  timestamp: string;
}

class DoorEventsStore {

  events: DoorEvent[] = [];
  connectionStatus: "connected" | "disconnected" = "disconnected";

  constructor() {
    makeAutoObservable(this);
  }

  addEvent(event: DoorEvent) {
    this.events = [event, ...this.events].slice(0, 10);
  }

  connectToSSE(url: string) {
    console.log("Connecting to SSE at:", url);
    const es = new EventSource(url);

    es.onopen = () => {
      this.connectionStatus = "connected";
      console.log("✅ SSE connected");
    };

    es.onmessage = (ev) => {
      try {
        const data = JSON.parse(ev.data);

        const event: DoorEvent = {
          badgeId: data.badgeId ?? null,
          firstName: data.firstName ?? "Unknown",
          lastName: data.lastName ,
          eventType: data.eventType,
          timestamp: new Date().toISOString(),
        };

        console.log("📨 SSE event received:", event);
        this.addEvent(event);

      } catch (err) {
        console.error("Error parsing SSE:", err);
      }
    };

    es.onerror = () => {
      this.connectionStatus = "disconnected";
      console.warn("⚠️ SSE connection lost");
    };
  }
}

export const doorEventsStore = new DoorEventsStore();