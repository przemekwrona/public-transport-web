import {Component, Input} from '@angular/core';
import {Stop} from "../../../generated/public-transport";

@Component({
    selector: 'app-bus-stop-editor',
    templateUrl: './bus-stop-editor.component.html',
    styleUrl: './bus-stop-editor.component.scss'
})
export class BusStopEditorComponent {

    @Input() public stop: Stop;

    constructor() {
    }

}
