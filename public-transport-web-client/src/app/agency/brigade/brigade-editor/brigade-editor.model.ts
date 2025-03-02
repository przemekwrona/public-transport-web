export class BrigadeModel {
    line: string = '';
    name: string = '';
    draggableElement: boolean = false;
    departure: any
    travelTimeInSeconds: number = 0;
    origin: string = '';
    destination: string = '';
    variant: string = '';
    variantDescription: string = '';
    isMainVariant: boolean = true;
}