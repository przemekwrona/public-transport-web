import {TripMode} from "../../../generated/public-transport-api";

export class BrigadeModel {
    line: string = '';
    name: string = '';
    variant: string = '';
    mode: TripMode | null = null;
    draggableElement: boolean = false;
    travelTimeInSeconds: number = 0;
    origin: string = '';
    destination: string = '';
    variantDescription: string = '';
    isMainVariant: boolean = true;
    departureTime: string = '';
}