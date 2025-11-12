import { useEffect, useState } from "react";
import type { DoorEvent } from "../stores/DoorEventsStore";

interface GateAnimationProps {
  lastEvent: DoorEvent | null;
}

export default function GateAnimation({ lastEvent }: GateAnimationProps) {
  const [isOpen, setIsOpen] = useState(false);

  useEffect(() => {
    if (lastEvent) {
      if (lastEvent.state === "open") {
        setIsOpen(true);
        // Auto-close after 5 seconds
        const timeout = setTimeout(() => setIsOpen(false), 5000);
        return () => clearTimeout(timeout);
      } else {
        setIsOpen(false);
      }
    }
  }, [lastEvent]);

  return (
    <div className="flex justify-center items-center p-8 bg-linear-to-b from-muted/50 to-background rounded-lg">
      <svg
        width="300"
        height="200"
        viewBox="0 0 300 200"
        className="drop-shadow-lg"
      >
        {/* Ground */}
        <rect x="0" y="180" width="300" height="20" fill="currentColor" className="text-muted" />

        {/* Gate Frame */}
        <rect x="50" y="50" width="10" height="130" fill="currentColor" className="text-foreground" />
        <rect x="240" y="50" width="10" height="130" fill="currentColor" className="text-foreground" />
        <rect x="50" y="50" width="200" height="10" fill="currentColor" className="text-foreground" />

        {/* Left Gate Door */}
        <g
          className="transition-transform duration-700 ease-in-out origin-left"
          style={{
            transform: isOpen ? "translateX(-90px)" : "translateX(0)",
            transformOrigin: "60px 60px",
          }}
        >
          <rect
            x="60"
            y="60"
            width="85"
            height="120"
            fill="currentColor"
            className="text-primary"
            stroke="currentColor"
            strokeWidth="2"
          />
          <rect x="70" y="70" width="15" height="100" fill="currentColor" className="text-muted" />
          <rect x="100" y="70" width="15" height="100" fill="currentColor" className="text-muted" />
          <rect x="130" y="70" width="10" height="100" fill="currentColor" className="text-muted" />
          <circle cx="135" cy="120" r="3" fill="currentColor" className="text-foreground" />
        </g>

        {/* Right Gate Door */}
        <g
          className="transition-transform duration-700 ease-in-out origin-right"
          style={{
            transform: isOpen ? "translateX(90px)" : "translateX(0)",
            transformOrigin: "240px 60px",
          }}
        >
          <rect
            x="155"
            y="60"
            width="85"
            height="120"
            fill="currentColor"
            className="text-primary"
            stroke="currentColor"
            strokeWidth="2"
          />
          <rect x="160" y="70" width="10" height="100" fill="currentColor" className="text-muted" />
          <rect x="185" y="70" width="15" height="100" fill="currentColor" className="text-muted" />
          <rect x="215" y="70" width="15" height="100" fill="currentColor" className="text-muted" />
          <circle cx="165" cy="120" r="3" fill="currentColor" className="text-foreground" />
        </g>

        {/* Status Light */}
        <circle
          cx="150"
          cy="30"
          r="8"
          fill="currentColor"
          className={isOpen ? "text-green-500" : "text-red-500"}
        >
          {isOpen && (
            <animate
              attributeName="opacity"
              values="1;0.5;1"
              dur="1s"
              repeatCount="indefinite"
            />
          )}
        </circle>
      </svg>
    </div>
  );
}
