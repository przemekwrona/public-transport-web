import {ChangeDetectionStrategy, Component, inject, model} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {DialogData} from "../bus-stop-selector/bus-stop-selector.component";

@Component({
  selector: 'app-bus-stop-modal-selector',
  templateUrl: './bus-stop-modal-selector.component.html',
  styleUrl: './bus-stop-modal-selector.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BusStopModalSelectorComponent {
  readonly dialogRef = inject(MatDialogRef<BusStopModalSelectorComponent>);
  readonly data = inject<DialogData>(MAT_DIALOG_DATA);
  readonly animal = model(this.data.animal);

  onNoClick(): void {
    this.dialogRef.close();
  }

}
