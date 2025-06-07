import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BusStopEditorComponent } from './bus-stop-editor.component';

describe('BusStopEditorComponent', () => {
  let component: BusStopEditorComponent;
  let fixture: ComponentFixture<BusStopEditorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BusStopEditorComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(BusStopEditorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
