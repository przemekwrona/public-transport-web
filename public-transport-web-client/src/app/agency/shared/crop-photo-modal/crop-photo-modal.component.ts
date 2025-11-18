import {Component, inject, OnInit} from '@angular/core';
import {CommonModule} from "@angular/common";
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from "@angular/material/dialog";
import {BusStopEditorComponent} from "../bus-stop-editor/bus-stop-editor.component";
import {StopsPatchRequest} from "../../../generated/public-transport-api";
import {BusStopModalEditorData} from "../bus-stop-modal-editor/bus-stop-modal-editor.component";

@Component({
  selector: 'app-crop-photo-modal',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    BusStopEditorComponent
  ],
  templateUrl: './crop-photo-modal.component.html',
  styleUrl: './crop-photo-modal.component.scss'
})
export class CropPhotoModalComponent implements OnInit {

  readonly dialogRef = inject(MatDialogRef<Blob>);
  readonly data = inject<BusStopModalEditorData>(MAT_DIALOG_DATA);

  ngOnInit(): void {
  }

  forceCloseModal() {
    this.dialogRef.close();
  }

  closeModal(event: StopsPatchRequest) {
    const busStop: BusStopModalEditorData = {} as BusStopModalEditorData;
    busStop.stopId = event.id;
    busStop.stopName = event.name;
    this.dialogRef.close(busStop);
  }


}
