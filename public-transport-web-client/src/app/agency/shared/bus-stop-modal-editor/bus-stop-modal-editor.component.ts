import {Component, inject, model, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";

export interface BusStopModalEditorData {
    stopId: number;
    stopName: string;
    lon: number;
    lat: number;
}

@Component({
    selector: 'app-bus-stop-modal-editor',
    templateUrl: './bus-stop-modal-editor.component.html',
    styleUrl: './bus-stop-modal-editor.component.scss'
})
export class BusStopModalEditorComponent implements OnInit {

    readonly dialogRef = inject(MatDialogRef<BusStopModalEditorComponent>);
    readonly data = inject<BusStopModalEditorData>(MAT_DIALOG_DATA);

    ngOnInit(): void {
    }

    closeModal(selectedStop: BusStopModalEditorData) {
        this.dialogRef.close(selectedStop);
    }

}
