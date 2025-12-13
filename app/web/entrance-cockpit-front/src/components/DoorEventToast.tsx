import { useEffect } from "react"
import { toast } from "sonner"
import { DoorOpen, DoorClosed } from "lucide-react"
import { useDoorEvents } from "../hooks/useDoorEvents"
import { doorEventsStore, type DoorEvent } from "../stores/DoorEventsStore"

export default function DoorEventToaster({ onEvent }: { onEvent?: (data: any) => void }) {
  const event: DoorEvent | null = useDoorEvents()
  useEffect(() => {
    if (!event) return;
    doorEventsStore.addEvent(event);
    onEvent?.({
      state: event.state,
      timestamp: new Date(event.timestamp).getTime(),
    })

    const Icon = event.state === "open" ? DoorOpen : DoorClosed
    const title = event.state === "open" ? "Door Opened" : "Door Closed"

    toast(title, {
      description: (
        <div className="flex items-center gap-2">
          <Icon className="h-4 w-4" />
          <span>
            {event.name} •{" "}
            {new Date(event.timestamp).toLocaleTimeString()}
          </span>
        </div>
      ),
      duration: 5000,
    })
  }, [event, onEvent])

  return null
}
