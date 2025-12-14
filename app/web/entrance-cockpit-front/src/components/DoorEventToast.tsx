import { useEffect } from "react"
import { toast } from "sonner"
import { DoorOpen } from "lucide-react"
import { useDoorEvents } from "../hooks/useDoorEvents"
import { doorEventsStore, type DoorEvent } from "../stores/DoorEventsStore"

export default function DoorEventToaster({ onEvent }: { onEvent?: (data: any) => void }) {
  const event: DoorEvent | null = useDoorEvents()
  useEffect(() => {
    if (!event) return;
    doorEventsStore.addEvent(event);
    onEvent?.({
      timestamp: new Date(event.timestamp).getTime(),
    })

    const title = event.eventType === 'badge' ? "Door Opened" : "Door Opened Manualy";

    toast(title, {
      description: (
        <div className="flex items-center gap-2">
          <DoorOpen className="h-4 w-4" />
          <span>
            {event.eventType === 'badge' && <p>{`${event.firstName} ${event.lastName}`} •{" "}</p>}
            {new Date(event.timestamp).toLocaleTimeString()}
          </span>
        </div>)
      ,
      duration: 3800,
    })
  }, [event, onEvent])

  return null
}
