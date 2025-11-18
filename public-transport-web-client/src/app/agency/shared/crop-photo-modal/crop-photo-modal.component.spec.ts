import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CropPhotoModalComponent } from './crop-photo-modal.component';

describe('CropPhotoModalComponent', () => {
  let component: CropPhotoModalComponent;
  let fixture: ComponentFixture<CropPhotoModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CropPhotoModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CropPhotoModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
