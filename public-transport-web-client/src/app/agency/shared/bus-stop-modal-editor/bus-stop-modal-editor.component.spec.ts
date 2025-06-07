import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BusStopModalEditorComponent } from './bus-stop-modal-editor.component';

describe('BusStopModalEditorComponent', () => {
  let component: BusStopModalEditorComponent;
  let fixture: ComponentFixture<BusStopModalEditorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BusStopModalEditorComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(BusStopModalEditorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
