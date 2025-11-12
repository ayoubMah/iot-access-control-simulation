import { useEffect, useState } from "react"
import { type DoorEvent } from "../stores/DoorEventsStore"

export function useDoorEvents() {
  const [eventData, setEventData] = useState<any>(null)

  useEffect(() => {
    const eventSource = new EventSource(import.meta.env.VITE_LOGS_BACKEND_SSE_URL)

    eventSource.onmessage = (event) => {
      try {
        const data: DoorEvent = JSON.parse(event.data)
        console.log("Received door event:", data)
        setEventData(data) 
      } catch (err) {
        console.error("Failed to parse SSE event:", err)
      }
    }

    eventSource.onerror = (err) => {
      console.error("SSE connection error:", err)
    }

    return () => {
      eventSource.close()
    }
  }, [])

  return eventData
}
