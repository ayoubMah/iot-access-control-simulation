import { makeAutoObservable, toJS } from "mobx";
export interface DoorEvent {
  name: string;
  badgeId: string;
  state: "open" | "close";
  timestamp: Date;
}
type ConnectionStatus = 'connected' | 'disconnected';

class DoorEventsStore {
    events?: DoorEvent[] = [];
    connectionStatus?: ConnectionStatus = 'connected';

    constructor() {
        makeAutoObservable(this);
    }

    getEvents = () => {
        return this.events?.slice().reverse();
    }

    addEvent = (event: DoorEvent) => {
        this.events?.push(event);
    }

    getConnectionStatus = () => {
        return this.connectionStatus;
    }

    setConnectionStatus = (status: ConnectionStatus) => {
        this.connectionStatus = status;
    }

};

export const doorEventsStore = new DoorEventsStore();