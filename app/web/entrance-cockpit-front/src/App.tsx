import { useEffect, useState } from "react";
import DoorStream from "./components/DoorStream";
import { autorun } from "mobx";
import DoorHistory from "./components/DoorHistory";
import GateAnimation from "./components/GateAnimation";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@radix-ui/react-tabs";
import ManualOperations from "./components/ManualOperations";
import { Toaster } from "sonner";
import DoorEventToast from "./components/DoorEventToast";
import { doorEventsStore, type DoorEvent } from "./stores/DoorEventsStore";
import { observer } from "mobx-react-lite";

function App() {
  const [lastEvent, setLastEvent] = useState<DoorEvent | null>(null);

  useEffect(() => {
    // 1️⃣ Connect to the SSE endpoint once when the app starts
    doorEventsStore.connectToSSE(import.meta.env.VITE_LOGS_BACKEND_SSE_URL);
  }, []);

  useEffect(() => {
    // 2️⃣ Subscribe to MobX store changes
    const disposer = autorun(() => {
      const events = doorEventsStore.events;
      if (events.length > 0) {
        setLastEvent(events[0]);
      }
    });

    // cleanup when component unmounts
    return () => disposer();
  }, []);
  return (
    <div className="min-h-screen bg-background">
      <div className="container mx-auto px-4 py-8 max-w-4xl">
        <div className="mb-8 text-center">
          <h1 className="text-4xl font-bold mb-2">Door Monitor</h1>
          <p className="text-muted-foreground">
            Real-time door access tracking
          </p>
        </div>

        <GateAnimation lastEvent={lastEvent} />

        <Tabs defaultValue="live" className="mt-8 w-full">
          <TabsList className="grid w-full grid-cols-3 bg-gray-100 p-1 rounded-xl shadow-sm">
            <TabsTrigger
              value="live"
              className="p-1 data-[state=active]:bg-white data-[state=active]:shadow-sm data-[state=active]:font-semibold rounded-lg transition-all"
            >
              Live Stream
            </TabsTrigger>
            <TabsTrigger
              value="history"
              className="p-1 data-[state=active]:bg-white data-[state=active]:shadow-sm data-[state=active]:font-semibold rounded-lg transition-all"
            >
              History
            </TabsTrigger>
            <TabsTrigger
              value="manual"
              className="p-1 data-[state=active]:bg-white data-[state=active]:shadow-sm data-[state=active]:font-semibold rounded-lg transition-all"
            >
              Manual Operations
            </TabsTrigger>
          </TabsList>

          <TabsContent value="live" className="mt-6 bg-gray-50 p-4 rounded-lg shadow-inner">
            <DoorStream />
          </TabsContent>

          <TabsContent value="history" className="mt-6 bg-gray-50 p-4 rounded-lg shadow-inner">
            <DoorHistory />
          </TabsContent>

          <TabsContent value="manual" className="mt-6 bg-gray-50 p-4 rounded-lg shadow-inner">
            <ManualOperations />
          </TabsContent>
        </Tabs>
      </div>
      <DoorEventToast />
      <Toaster />
    </div>
  );
}

export default observer(App);