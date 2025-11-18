import {Component, inject, OnInit} from '@angular/core';
import {CommonModule} from "@angular/common";
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from "@angular/material/dialog";
import {ImageCroppedEvent, ImageCropperComponent, LoadedImage} from "ngx-image-cropper";

export interface ImageEvent {
    event: Event;
}

@Component({
    selector: 'app-crop-photo-modal',
    standalone: true,
    imports: [
        CommonModule,
        MatDialogModule,
        ImageCropperComponent
    ],
    templateUrl: './crop-photo-modal.component.html',
    styleUrl: './crop-photo-modal.component.scss'
})
export class CropPhotoModalComponent implements OnInit {

    readonly dialogRef = inject(MatDialogRef<CropPhotoModalComponent>);
    readonly data: ImageEvent = inject<ImageEvent>(MAT_DIALOG_DATA);

    private imageChangedEvent: ImageCroppedEvent;

    constructor() {
    }

    ngOnInit(): void {
    }

    forceCloseModal() {
        this.dialogRef.close();
    }

    closeModal() {
        this.dialogRef.close(this.imageChangedEvent);
    }

    async imageCropped(event: ImageCroppedEvent) {
        this.imageChangedEvent = event;
    }

    imageLoaded(image: LoadedImage) {
        // show cropper
    }

    cropperReady() {
        // cropper ready
    }

    loadImageFailed() {
        // show message
    }


}
