import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BrigadeCreatorModalComponent } from './brigade-creator-modal.component';

describe('BrigadeCreatorModalComponent', () => {
  let component: BrigadeCreatorModalComponent;
  let fixture: ComponentFixture<BrigadeCreatorModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BrigadeCreatorModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BrigadeCreatorModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
